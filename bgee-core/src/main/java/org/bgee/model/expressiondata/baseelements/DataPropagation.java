package org.bgee.model.expressiondata.baseelements;

/**
 * Defines the source of expression data of a {@link CallData} along 
 * the ontologies used to capture conditions. For instance, the expression of a gene 
 * in a given anatomical entity could have been observed in the anatomical entity itself, 
 * or only in some substructures of the entity, or in both. Similarly, expression in a given 
 * developmental stage could have been observed only in a sub-stage, or in the stage itself, 
 * etc. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Sept. 2015
 * @since Bgee 13 Sept. 2015
 *
 */
public class DataPropagation {
    public static enum PropagationState {
        SELF, PARENT, CHILD, SELF_AND_PARENT, SELF_AND_CHILD, 
        SELF_OR_PARENT, SELF_OR_CHILD;
    }
    
    private final PropagationState anatEntityPropagationState;
    private final PropagationState devStagePropagationState;
    
    public DataPropagation() {
        this(PropagationState.SELF, PropagationState.SELF);
    }
    public DataPropagation(PropagationState anatEntityPropagationState, 
            PropagationState devStagePropagationState) {
        this.anatEntityPropagationState = anatEntityPropagationState;
        this.devStagePropagationState   = devStagePropagationState;
    }
    
    /**
     * @return the anatEntityPropagationState
     */
    public PropagationState getAnatEntityPropagationState() {
        return anatEntityPropagationState;
    }
    /**
     * @return the devStagePropagationState
     */
    public PropagationState getDevStagePropagationState() {
        return devStagePropagationState;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((anatEntityPropagationState == null) ? 0
                        : anatEntityPropagationState.hashCode());
        result = prime
                * result
                + ((devStagePropagationState == null) ? 0
                        : devStagePropagationState.hashCode());
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
        if (!(obj instanceof DataPropagation)) {
            return false;
        }
        DataPropagation other = (DataPropagation) obj;
        if (anatEntityPropagationState != other.anatEntityPropagationState) {
            return false;
        }
        if (devStagePropagationState != other.devStagePropagationState) {
            return false;
        }
        return true;
    }
    @Override
    public String toString() {
        return "DataPropagation [anatEntity="
                + anatEntityPropagationState
                + ", devStage=" + devStagePropagationState
                + "]";
    }
    
}