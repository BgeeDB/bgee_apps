package org.bgee.model.expressiondata.baseelements;

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
 * <li>{@code UNKNOWN}: data observed either in the condition element itself, 
 * or in some descendant or some parent of the condition element.
 * </ul>
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14 Mar. 2017
 * @see     DataPropagation
 * @see     ExperimentExpressionCount
 * @since   Bgee 13, Sept. 2015
 */
public enum PropagationState {
    SELF(true), ANCESTOR(false), DESCENDANT(false),
    SELF_AND_ANCESTOR(true), SELF_AND_DESCENDANT(true), ANCESTOR_AND_DESCENDANT(false),
    SELF_OR_ANCESTOR(null), SELF_OR_DESCENDANT(null),
    ALL(true), UNKNOWN(null);
    
    private final Boolean includingObservedData;
    
    private PropagationState(Boolean includingObservedData) {
        this.includingObservedData = includingObservedData;
    }

    /**
     * @return  A {@code Boolean} that is {@code true} if this {@code PropagationState} includes
     *          observed data, {@code false} if it does not, {@code null} if this {@code PropagationState}
     *          does not allow to determine this information.
     */
    public Boolean isIncludingObservedData() {
        return includingObservedData;
    }
}
