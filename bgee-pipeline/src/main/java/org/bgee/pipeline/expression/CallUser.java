package org.bgee.pipeline.expression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
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
}
