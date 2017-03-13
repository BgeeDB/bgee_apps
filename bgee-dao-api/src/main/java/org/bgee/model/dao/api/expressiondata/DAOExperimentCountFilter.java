package org.bgee.model.dao.api.expressiondata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class allowing to filter the retrieval of {@link GlobalExpressionCallTO}s
 * based on count of experiments supporting the call.
 * 
 * @author Frederic Bastian
 * @version Bgee 14 Mar. 2017
 * @see GlobalExpressionCallDAO
 * @since Bgee 14 Mar. 2017
 */
public class DAOExperimentCountFilter {
    private final static Logger log = LogManager.getLogger(DAOExperimentCountFilter.class.getName());

    public static enum CallType {
        PRESENT, ABSENT;
    }
    public static enum DataQuality {
        LOW, HIGH;
    }
    public static enum PropagationState {
        ALL, SELF, ANCESTOR, DESCENDANT;
    }
    public static enum Qualifier {
        EQUALS_TO, GREATER_THAN, LESS_THAN;
    }

    private final CallType callType;
    private final DataQuality dataQuality;
    private final PropagationState propagationState;
    private final Qualifier qualifier;
    private final int count;
    
    /**
     * 
     * @param callType
     * @param dataQuality
     * @param propagationState
     * @param qualifier
     * @param count
     * @throws IllegalArgumentException If any argument is null, or {@code count} is negative.
     */
    public DAOExperimentCountFilter(CallType callType, DataQuality dataQuality,
            PropagationState propagationState, Qualifier qualifier, int count)
                    throws IllegalArgumentException {
        log.entry(callType, dataQuality, propagationState, qualifier, count);
        
        if (callType == null || dataQuality == null || propagationState == null || qualifier == null) {
            throw log.throwing(new IllegalArgumentException("No argument can be null"));
        }
        if (count <= 0) {
            throw log.throwing(new IllegalArgumentException("Count cannot be negative."));
        }
        
        this.callType = callType;
        this.dataQuality = dataQuality;
        this.propagationState = propagationState;
        this.qualifier = qualifier;
        this.count = count;
        
        log.exit();
    }

    public CallType getCallType() {
        return callType;
    }
    public DataQuality getDataQuality() {
        return dataQuality;
    }
    public PropagationState getPropagationState() {
        return propagationState;
    }
    public Qualifier getQualifier() {
        return qualifier;
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
        result = prime * result + ((propagationState == null) ? 0 : propagationState.hashCode());
        result = prime * result + ((qualifier == null) ? 0 : qualifier.hashCode());
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
        DAOExperimentCountFilter other = (DAOExperimentCountFilter) obj;
        if (callType != other.callType) {
            return false;
        }
        if (count != other.count) {
            return false;
        }
        if (dataQuality != other.dataQuality) {
            return false;
        }
        if (propagationState != other.propagationState) {
            return false;
        }
        if (qualifier != other.qualifier) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DAOExperimentCountFilter [callType=").append(callType)
               .append(", dataQuality=").append(dataQuality)
               .append(", propagationState=").append(propagationState)
               .append(", qualifier=").append(qualifier)
               .append(", count=").append(count).append("]");
        return builder.toString();
    }
}
