package org.bgee.model.expressiondata.baseelements;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.baseelements.DataPropagation.PropagationState;

/**
 * Defines the source of a {@code CallData} count.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2017
 * @since   Bgee 14, Mar. 2017
 */
public class CountType {

    private final static Logger log = LogManager.getLogger(CountType.class.getName());

    /**
     * @see #getCallType()
     */
    CallType callType;
    
    /**
     * @see #getDataQuality()
     */
    DataQuality dataQuality;
    
    /**
     * @see #getPropagationState()
     */
    PropagationState propagationState;
    
    /**
     * Instantiate a new {@code CountType} with a {@code CallType}, a {@code DataQuality},
     * and a {@code PropagationState}.
     * <p>
     * Only {@code PropagationState.SELF}, {@code PropagationState.ANCESTOR},
     * {@code PropagationState.DESCENDANT} and {@code PropagationState.ALL} are allowed.
     */
    public CountType(CallType callType, DataQuality dataQuality, PropagationState propagationState) {
        final Set<PropagationState> allowedPropStates = new HashSet<>(Arrays.asList(
            PropagationState.SELF, PropagationState.DESCENDANT, PropagationState.ANCESTOR,
            PropagationState.ALL));
        if (propagationState != null && !allowedPropStates.contains(propagationState)) {
            throw log.throwing(new IllegalArgumentException("The provided propagation state ("
                + propagationState + ") is not allowed"));
        }
        
        // FIXME add sanity checks (for instance, absent/low/desc is not allowed) 
        this.callType = callType;
        this.dataQuality = dataQuality;
        this.propagationState = propagationState;
    }

    /**
     * @return  The {@code CallType} that is the call type associated to the count.
     */
    public CallType getCallType() {
        return callType;
    }

    /**
     * @return  The {@code DataQuality} that is the quality associated to the count.
     */
    public DataQuality getDataQuality() {
        return dataQuality;
    }

    /**
     * @return  The {@code PropagationState} describing how count were propagated.
     */
    public PropagationState getPropagationState() {
        return propagationState;
    }

    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((callType == null) ? 0 : callType.hashCode());
        result = prime * result + ((dataQuality == null) ? 0 : dataQuality.hashCode());
        result = prime * result + ((propagationState == null) ? 0 : propagationState.hashCode());
        return result;
    }

    @Override
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
        CountType other = (CountType) obj;
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
        if (propagationState != other.propagationState) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CountType [callType=" + callType + ", dataQuality=" + dataQuality + 
                    ", propagationState=" + propagationState + "]";
    }
}
