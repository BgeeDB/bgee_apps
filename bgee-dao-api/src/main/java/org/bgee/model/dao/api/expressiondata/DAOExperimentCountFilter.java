package org.bgee.model.dao.api.expressiondata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.DAOExperimentCount.CallType;
import org.bgee.model.dao.api.expressiondata.DAOExperimentCount.DataQuality;
import org.bgee.model.dao.api.expressiondata.DAOPropagationState;

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

    
    public static enum Qualifier {
        EQUALS_TO, GREATER_THAN, LESS_THAN;
    }

    private final DAOExperimentCount expCount;
    private final Qualifier qualifier;
    
    public DAOExperimentCountFilter(CallType callType, DataQuality dataQuality,
            DAOPropagationState daoPropagationState, Qualifier qualifier, int count) {
        this(new DAOExperimentCount(callType, dataQuality, daoPropagationState, count), qualifier);
    }
    //dependency injection of DAOExperimentCount rather than inheritance
    public DAOExperimentCountFilter(DAOExperimentCount expCount, Qualifier qualifier)
                    throws IllegalArgumentException {
        log.entry(expCount, qualifier);
        
        if (expCount == null || qualifier == null) {
            throw log.throwing(new IllegalArgumentException("No argument can be null"));
        }
        
        this.expCount = expCount;
        this.qualifier = qualifier;
        
        log.traceExit();
    }

    public CallType getCallType() {
        return expCount.getCallType();
    }
    public DataQuality getDataQuality() {
        return expCount.getDataQuality();
    }
    public DAOPropagationState getPropagationState() {
        return expCount.getPropagationState();
    }
    public Qualifier getQualifier() {
        return qualifier;
    }
    public int getCount() {
        return expCount.getCount();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((expCount == null) ? 0 : expCount.hashCode());
        //manage count value of DAOExperimentCount directly here
        result = prime * result + ((expCount == null) ? 0 : expCount.getCount());
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
        if (expCount == null) {
            if (other.expCount != null) {
                return false;
            }
        } else if (!expCount.equals(other.expCount)) {
            return false;
        }
        //manage count value of DAOExperimentCount directly here
        if (expCount != null && expCount.getCount() != other.getCount()) {
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
        builder.append("DAOExperimentCountFilter [qualifier=").append(qualifier)
               .append(", experimentCount=").append(expCount)
               .append("]");
        return builder.toString();
    }
}
