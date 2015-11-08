package org.bgee.model.expressiondata.baseelements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Defines the source of expression data of a {@link CallData} along 
 * the ontologies used to capture conditions. For instance, the expression of a gene 
 * in a given anatomical entity could have been observed in the anatomical entity itself, 
 * or only in some substructures of the entity, or in both. Similarly, expression in a given 
 * developmental stage could have been observed only in a sub-stage, or in the stage itself, 
 * etc. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Nov. 2015
 * @since Bgee 13 Sept. 2015
 *
 */
public class DataPropagation {
    private final static Logger log = LogManager.getLogger(DataPropagation.class.getName());
    
    /**
     * An {@code Enum} describing the different methods of call propagation available, 
     * along any ontology used to capture conditions. 
     * <ul>
     * <li>{@code SELF}: no propagation, data observed in the condition element itself.
     * <li>{@code ANCESTOR}: data observed in some ancestor of the condition element.
     * <li>{@code DESCENDANT}: data observed in some descendant of the condition element.
     * <li>{@code SELF_AND_ANCESTOR}: data observed both in the condition element itself, 
     * and in some ancestor of the condition element.
     * <li>{@code SELF_AND_DESCENDANT}: data observed both in the condition element itself, 
     * and in some descendant of the condition element.
     * <li>{@code SELF_OR_ANCESTOR}: data observed either in the condition element itself, 
     * or in some ancestor of the condition element.
     * <li>{@code SELF_OR_DESCENDANT}: data observed either in the condition element itself, 
     * or in some descendant of the condition element.
     * </ul>
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Nov. 2015
     * @see DataPropagation
     * @since Bgee 13 Sept. 2015
     */
    public static enum PropagationState {
        SELF, ANCESTOR, DESCENDANT, SELF_AND_ANCESTOR, SELF_AND_DESCENDANT, 
        SELF_OR_ANCESTOR, SELF_OR_DESCENDANT;
    }
    
    /**
     * @see #getAnatEntityPropagationState()
     */
    private final PropagationState anatEntityPropagationState;
    /**
     * @see #getDevStagePropagationState()
     */
    private final PropagationState devStagePropagationState;
    
    /**
     * Instantiate a new {@code DataPropagation} with a {@code PropagationState.SELF} state 
     * for all condition elements.
     * @see #DataPropagation(PropagationState, PropagationState)
     */
    public DataPropagation() {
        this(PropagationState.SELF, PropagationState.SELF);
    }
    /**
     * Instantiate a new {@code DataPropagation} by providing the propagation state along anatomy, 
     * and the propagation state along developmental stages. 
     * 
     * @param anatEntityPropagationState    A {@code PropagationState} describing how data 
     *                                      are propagated along anatomy.
     * @param devStagePropagationState      A {@code PropagationState} describing how data 
     *                                      are propagated along dev. stages.
     * @throws IllegalArgumentException     If any of the arguments is {@code null}.
     */
    public DataPropagation(PropagationState anatEntityPropagationState, 
            PropagationState devStagePropagationState) throws IllegalArgumentException {
        if (anatEntityPropagationState == null || devStagePropagationState == null) {
            throw log.throwing(new IllegalArgumentException("No argument can be null"));
        }
        this.anatEntityPropagationState = anatEntityPropagationState;
        this.devStagePropagationState   = devStagePropagationState;
    }
    
    /**
     * @return  The {@code PropagationState} describing how data are propagated along anatomy.
     */
    public PropagationState getAnatEntityPropagationState() {
        return anatEntityPropagationState;
    }
    /**
     * @return  The {@code PropagationState} describing how data are propagated along 
     *          developmental stages.
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