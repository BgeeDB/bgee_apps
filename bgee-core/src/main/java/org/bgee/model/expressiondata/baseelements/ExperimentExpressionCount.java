package org.bgee.model.expressiondata.baseelements;

import java.util.EnumSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.baseelements.PropagationState;

/**
 * This class allows to store the number of experiments that participated in the production
 * of a given global summary {@link org.bgee.model.expressiondata.call.Call.ExpressionCall ExpressionCall}.
 * Experiments participating in the production of a call can produce different {@code CallType}s
 * and {@code DataQuality}s, that are all together taken into account to define the global summary
 * {@code ExpressionCall}. Objects of this class should be associated to the {@code DataType}
 * of the experiments (most likely in an 
 * {@link org.bgee.model.expressiondata.call.CallData.ExpressionCallData ExpressionCallData} object).
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 14, Mar. 2017
 * @since   Bgee 14, Mar. 2017
 */
public class ExperimentExpressionCount {
    private final static Logger log = LogManager.getLogger(ExperimentExpressionCount.class.getName());
    
    /**
     * A {@code Set} of {@code PropagationState}s that are accepted by this class.
     */
    public final static Set<PropagationState> ALLOWED_PROP_STATES = EnumSet.of(
            //Note: as of Bgee 14.2, we do not propagate absent expression calls to substructures anymore
            PropagationState.SELF, PropagationState.DESCENDANT, /*PropagationState.ANCESTOR,*/
            PropagationState.ALL);

    /**
     * @see #getCallType()
     */
    private final CallType.Expression callType;
    /**
     * @see #getDataQuality()
     */
    private final DataQuality dataQuality;
    /**
     * @see #getPropagationState()
     */
    private final PropagationState propagationState;
    /**
     * @see #getCount()
     */
    private final int count;
    
    /**
     * Instantiate a new {@code ExperimentExpressionCount} with a {@code CallType}, a {@code DataQuality},
     * and a {@code PropagationState}.
     * <p>
     * Only {@code PropagationState.SELF}, {@code PropagationState.ANCESTOR},
     * {@code PropagationState.DESCENDANT} and {@code PropagationState.ALL} are allowed.
     */
    //Use CallType.Expression for now, this could be changed in the future to CallType
    public ExperimentExpressionCount(CallType.Expression callType, DataQuality dataQuality, 
            PropagationState propagationState, int experimentCount) {
        log.entry(callType, dataQuality, propagationState, experimentCount);
        if (!ALLOWED_PROP_STATES.contains(propagationState)) {
            throw log.throwing(new IllegalArgumentException("The provided propagation state ("
                + propagationState + ") is not allowed"));
        }
        if (callType == null) {
            throw log.throwing(new IllegalArgumentException("callType cannot be null"));
        }
        if (dataQuality == null) {
            throw log.throwing(new IllegalArgumentException("dataQuality cannot be null"));
        }
        if (experimentCount < 0) {
            throw log.throwing(new IllegalArgumentException("Invalid experiment count: " 
                    + experimentCount));
        }
        callType.checkPropagationState(propagationState);
        
        this.callType = callType;
        this.dataQuality = dataQuality;
        this.propagationState = propagationState;
        this.count = experimentCount;
    }

    /**
     * @return  The {@code CallType} that the experiments counted in this object produced.
     */
    public CallType.Expression getCallType() {
        return callType;
    }
    /**
     * @return  The {@code DataQuality} of the call that the experiments counted in this object produced.
     */
    public DataQuality getDataQuality() {
        return dataQuality;
    }
    /**
     * @return  The {@code PropagationState} describing how the data produced by 
     *          the experiments counted in this object were propagated to be included
     *          in the related global summary {@code ExpressionCall}.
     */
    public PropagationState getPropagationState() {
        return propagationState;
    }
    /**
     * @return  An {@code int} that is the number of experiments that produced the calls described
     *          in this object by their {@code CallType} and {@code DataQuality}, with an origin
     *          related to the global summary {@code ExpressionCall} described by the {@code PropagationState}.
     */
    public int getCount() {
        return count;
    }

    @Override
    //TODO: remove count from hashCode/equals after reactivating unit tests
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((callType == null) ? 0 : callType.hashCode());
        result = prime * result + ((dataQuality == null) ? 0 : dataQuality.hashCode());
        result = prime * result + count;
        result = prime * result + ((propagationState == null) ? 0 : propagationState.hashCode());
        return result;
    }
    @Override
    //TODO: remove count from hashCode/equals after reactivating unit tests
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ExperimentExpressionCount other = (ExperimentExpressionCount) obj;
        if (callType == null) {
            if (other.callType != null) {
                return false;
            }
        } else if (!callType.equals(other.callType)) {
            return false;
        }
        if (dataQuality != other.dataQuality) {
            return false;
        }
        if (count != other.count) {
            return false;
        }
        if (propagationState != other.propagationState) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ExperimentExpressionCount [callType=").append(callType).append(", dataQuality=")
                .append(dataQuality).append(", propagationState=").append(propagationState)
                .append(", count=").append(count).append("]");
        return builder.toString();
    }
}
