package org.bgee.model.expressiondata.baseelements;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Defines the source of expression data of a {@link CallData} or {@code Call}, along 
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
//TODO: actually, if we really wanted to abstract away details about what elements 
//compose a condition, we should use an Enum describing the condition elements 
//(e.g., ANAT_ENTITY, DEV_STAGE, ...).
//The constructor could accept a Map ConditionElement -> PropagationState. And a sanity check 
//could be performed to ensure that all ConditionElement enum elements are in the key set of the Map.
//If we don't want to change the class signature, we could keep the getAnatEntityPropagationState etc 
//as helper methods. 
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
     * <li>{@code ANCESTOR_AND_DESCENDANT}: data observed both in some descendant, 
     * and in some ancestor of the condition element.
     * <li>{@code ALL}: data observed both in the condition element itself, 
     * and in some descendant of the condition element, and in some ancestor of the condition element.
     * </ul>
     * 
     * @author  Frederic Bastian
     * @author  Valentine Rech de Laval
     * @version Bgee 13, May 2016
     * @see     DataPropagation
     * @since   Bgee 13, Sept. 2015
     */
    public static enum PropagationState {
        SELF, ANCESTOR, DESCENDANT, SELF_AND_ANCESTOR, SELF_AND_DESCENDANT, 
        SELF_OR_ANCESTOR, SELF_OR_DESCENDANT, ANCESTOR_AND_DESCENDANT, ALL;
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
     * @see #getIncludingObservedData()
     */
    private final Boolean includingObservedData;
    
    /**
     * Instantiate a new {@code DataPropagation} with a {@code PropagationState.SELF} state 
     * for all condition elements.
     * @see #DataPropagation(PropagationState, PropagationState)
     * @see #DataPropagation(PropagationState, PropagationState, Boolean)
     */
    public DataPropagation() {
        this(PropagationState.SELF, PropagationState.SELF);
    }
    /**
     * Instantiate a new {@code DataPropagation} by providing the propagation state along anatomy, 
     * and the propagation state along developmental stages. The observed data state 
     * is unknown.
     * 
     * @param anatEntityPropagationState    A {@code PropagationState} describing how data 
     *                                      are propagated along anatomy.
     * @param devStagePropagationState      A {@code PropagationState} describing how data 
     *                                      are propagated along dev. stages.
     * @throws IllegalArgumentException     If any of the arguments is {@code null}.
     * @see #DataPropagation(PropagationState, PropagationState, Boolean)
     */
    public DataPropagation(PropagationState anatEntityPropagationState, 
            PropagationState devStagePropagationState) throws IllegalArgumentException {
        this(anatEntityPropagationState, devStagePropagationState, null);
    }
    /**
     * Instantiate a new {@code DataPropagation} by providing the propagation state along anatomy, 
     * the propagation state along developmental stages, and the observed data state. 
     * If {@code includingObservedData} is {@code null}, it means that the observed data state 
     * is unknown. 
     * 
     * @param anatEntityPropagationState    A {@code PropagationState} describing how data 
     *                                      are propagated along anatomy.
     * @param devStagePropagationState      A {@code PropagationState} describing how data 
     *                                      are propagated along dev. stages.
     * @param includingObservedData         A {@code Boolean} defining whether the data includes 
     *                                      some that were observed in the condition itself, 
     *                                      and not only in an ancestor or a descendant. 
     *                                      If {@code null}, it means that this information is unknown 
     *                                      (or not requested, if used as part of a {@code CallFilter}).
     * @throws IllegalArgumentException     If {@code anatEntityPropagationState} or 
     *                                      {@code devStagePropagationState} is {@code null}.
     */
    public DataPropagation(PropagationState anatEntityPropagationState, 
            PropagationState devStagePropagationState, Boolean includingObservedData) 
                    throws IllegalArgumentException {
        if (anatEntityPropagationState == null || devStagePropagationState == null) {
            throw log.throwing(new IllegalArgumentException("The propagation states cannot be null"));
        }
        //check consistency of the PropagationState and of the observed data state
        PropagationState[] states = new PropagationState[]{
                anatEntityPropagationState, devStagePropagationState};
        if (new Boolean(true).equals(includingObservedData) && Arrays.stream(states).anyMatch(
                e -> PropagationState.ANCESTOR.equals(e) || PropagationState.DESCENDANT.equals(e) || 
                PropagationState.ANCESTOR_AND_DESCENDANT.equals(e)) ||
                
            // Here, we cannot check consistency of PropagationState.SELF_AND_ANCESTOR,
            // PropagationState.SELF_AND_DESCENDANT, and PropagationState.ALL due to 
            // reconciliation of calls. For instance, if we reconcile following ExpressionCalls:
            // - call with anat. entity propa. state equals to SELF and 
            // dev. stage propa. state equals to ANCESTOR
            // - call with anat. entity propa. state equals to ANCESTOR and 
            // dev. stage propa. state equals to SELF
            // ExpressionCall will have anat. entity propa. state equals to SELF_AND_ANCESTOR and 
            // dev. stage propa. state equals to SELF_AND_ANCESTOR
            // with includingObservedData equals to true
            new Boolean(false).equals(includingObservedData) && Arrays.stream(states).allMatch(
                e -> PropagationState.SELF.equals(e))) {
            
            throw log.throwing(new IllegalArgumentException("The provided observed data state ("
                    + includingObservedData + ") is incompatible with the provided PropagationStates ("
                    + "anatomy: " + anatEntityPropagationState + " - stage: " + devStagePropagationState));
        } 
        this.anatEntityPropagationState = anatEntityPropagationState;
        this.devStagePropagationState   = devStagePropagationState;
        this.includingObservedData      = includingObservedData;
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
    /**
     * @return  A {@code Boolean} defining whether the data includes some that were observed 
     *          in the condition itself, and not only in an ancestor or a descendant. 
     *          If {@code null}, it means that this information is unknown.  
     */
    public Boolean getIncludingObservedData() {
        return includingObservedData;
    }
    
    /**
     * @return  A {@code Set} of {@code PropagationState}s that are all the states 
     *          associated to all condition elements. 
     */
    //this method is useful to abstract away what are the elements defining a condition.
    public EnumSet<PropagationState> getAllPropagationStates() {
        return Stream.of(anatEntityPropagationState, devStagePropagationState)
        .collect(Collectors.toCollection(() -> EnumSet.noneOf(PropagationState.class)));
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((anatEntityPropagationState == null) ? 0 : anatEntityPropagationState.hashCode());
        result = prime * result + ((devStagePropagationState == null) ? 0 : devStagePropagationState.hashCode());
        result = prime * result + ((includingObservedData == null) ? 0 : includingObservedData.hashCode());
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
        DataPropagation other = (DataPropagation) obj;
        if (anatEntityPropagationState != other.anatEntityPropagationState) {
            return false;
        }
        if (devStagePropagationState != other.devStagePropagationState) {
            return false;
        }
        if (includingObservedData == null) {
            if (other.includingObservedData != null) {
                return false;
            }
        } else if (!includingObservedData.equals(other.includingObservedData)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DataPropagation [anatEntityPropagationState=").append(anatEntityPropagationState)
                .append(", devStagePropagationState=").append(devStagePropagationState)
                .append(", includingObservedData=").append(includingObservedData).append("]");
        return builder.toString();
    }
}