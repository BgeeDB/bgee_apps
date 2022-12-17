package org.bgee.model.dao.api.expressiondata.call;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.TransferObject.EnumDAOField;

/**
 * {@code Enum} listing the propagation states used in Bgee database:
 *
 * <ul>
 * <li>{@code ALL}
 * <li>{@code SELF}
 * <li>{@code ANCESTOR}
 * <li>{@code DESCENDANT}
 * <li>{@code SELF_AND_ANCESTOR}
 * <li>{@code SELF_AND_DESCENDANT}
 * <li>{@code ANCESTOR_AND_DESCENDANT}
 * </ul>
 * 
 * @author Freeric Bastian
 * @version Bgee 14 Mar. 2017
 * @since Bgee 14 Mar. 2017
 */
public enum DAOPropagationState implements EnumDAOField {
    ALL("all", true), SELF("self", true), ANCESTOR("ancestor", false), DESCENDANT("descendant", false),
    SELF_AND_ANCESTOR("self and ancestor", true), SELF_AND_DESCENDANT("self and descendant", true),
    ANCESTOR_AND_DESCENDANT("ancestor and descendant", false);
    private final static Logger log = LogManager.getLogger(DAOPropagationState.class.getName());

    /**
     * Convert the {@code String} representation of a propagation state into a {@code DAOPropagationState}.
     * Operation performed by calling {@link TransferObject#convert(Class, String)} 
     * with {@code DAOPropagationState} as the {@code Class} argument, and {@code representation} as 
     * the {@code String} argument.
     * 
     * @param representation    A {@code String} representing a propagation state.
     * @return                  The {@code DAOPropagationState} corresponding to {@code representation}.
     * @throws IllegalArgumentException If {@code representation} does not correspond 
     *                                  to any {@code DAOPropagationState}.
     */
    public static final DAOPropagationState convertToPropagationState(String representation) {
        log.entry(representation);
        return log.traceExit(TransferObject.convert(DAOPropagationState.class, representation));
    }

    private final boolean observedState;
    /**
     * See {@link #getStringRepresentation()}
     */
    private final String stringRepresentation;

    /**
     * Constructor providing the {@code String} representation of this {@code DAOPropagationState}.
     * 
     * @param stringRepresentation  A {@code String} corresponding to this {@code DAOPropagationState}.
     * @param observedState         See {@link #getObservedState()}.
     */
    private DAOPropagationState(String stringRepresentation, boolean observedState) {
        this.stringRepresentation = stringRepresentation;
        this.observedState = observedState;
    }
    /**
     * @return  A {@code boolean} defining whether this {@code DAOPropagationState} is a state
     *          <strong>including</strong> observed data. If {@code true} it includes observed data,
     *          if {@code false} it does not include observed data.
     */
    public boolean getObservedState() {
        return observedState;
    }
    @Override
    public String getStringRepresentation() {
        return this.stringRepresentation;
    }
    @Override
    public String toString() {
        return this.getStringRepresentation();
    }
}
