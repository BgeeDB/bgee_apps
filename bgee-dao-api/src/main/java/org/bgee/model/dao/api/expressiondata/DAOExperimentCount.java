package org.bgee.model.dao.api.expressiondata;

import java.util.EnumSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class allowing to store the count of experiments in support of {@link GlobalExpressionCallTO}s.
 * 
 * @author Frederic Bastian
 * @version Bgee 14 Mar. 2017
 * @see GlobalExpressionCallDAO.GlobalExpressionCallDataTO
 * @see DAOExperimentCountFilter
 * @see GlobalExpressionCallDAO
 * @since Bgee 14 Mar. 2017
 */
public class DAOExperimentCount {
    private final static Logger log = LogManager.getLogger(DAOExperimentCount.class.getName());

    public static enum CallType {
        PRESENT, ABSENT;
    }
    public static enum DataQuality {
        LOW, HIGH;
    }
    
    public static final Set<DAOPropagationState> ALLOWED_PROP_STATES = EnumSet.of(
            DAOPropagationState.ALL, DAOPropagationState.SELF,
            DAOPropagationState.ANCESTOR, DAOPropagationState.DESCENDANT);
    

    private final CallType callType;
    private final DataQuality dataQuality;
    private final DAOPropagationState daoPropagationState;
    private final int count;

    /**
     * 
     * @param callType
     * @param dataQuality
     * @param daoPropagationState
     * @param qualifier
     * @param count
     * @throws IllegalArgumentException If any argument is null, or {@code count} is negative.
     */
    public DAOExperimentCount(CallType callType, DataQuality dataQuality,
            DAOPropagationState daoPropagationState, int count)
                    throws IllegalArgumentException {
        log.entry(callType, dataQuality, daoPropagationState, count);
        
        if (callType == null || dataQuality == null || daoPropagationState == null) {
            throw log.throwing(new IllegalArgumentException("No argument can be null"));
        }
        if (!ALLOWED_PROP_STATES.contains(daoPropagationState)) {
            throw log.throwing(new IllegalArgumentException(
                    "Invalid DAOPropagationState: " + daoPropagationState));
        }
        if (count < 0) {
            throw log.throwing(new IllegalArgumentException("Count cannot be negative."));
        }
        
        this.callType = callType;
        this.dataQuality = dataQuality;
        this.daoPropagationState = daoPropagationState;
        this.count = count;
        
        log.exit();
    }

    public CallType getCallType() {
        return callType;
    }
    public DataQuality getDataQuality() {
        return dataQuality;
    }
    public DAOPropagationState getPropagationState() {
        return daoPropagationState;
    }
    public int getCount() {
        return count;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((callType == null) ? 0 : callType.hashCode());
        result = prime * result + count;
        result = prime * result + ((dataQuality == null) ? 0 : dataQuality.hashCode());
        result = prime * result + ((daoPropagationState == null) ? 0 : daoPropagationState.hashCode());
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
        DAOExperimentCount other = (DAOExperimentCount) obj;
        if (callType != other.callType) {
            return false;
        }
        if (count != other.count) {
            return false;
        }
        if (dataQuality != other.dataQuality) {
            return false;
        }
        if (daoPropagationState != other.daoPropagationState) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DAOExperimentCountFilter [callType=").append(callType)
               .append(", dataQuality=").append(dataQuality)
               .append(", daoPropagationState=").append(daoPropagationState)
               .append(", count=").append(count).append("]");
        return builder.toString();
    }
}
