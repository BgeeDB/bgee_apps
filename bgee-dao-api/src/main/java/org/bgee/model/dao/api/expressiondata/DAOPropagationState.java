package org.bgee.model.dao.api.expressiondata;

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
    ALL("all"), SELF("self"), ANCESTOR("ancestor"), DESCENDANT("descendant"),
    SELF_AND_ANCESTOR("self and ancestor"), SELF_AND_DESCENDANT("self and descendant"),
    ANCESTOR_AND_DESCENDANT("ancestor and descendant");
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
        return log.exit(TransferObject.convert(DAOPropagationState.class, representation));
    }

    /**
     * See {@link #getStringRepresentation()}
     */
    private final String stringRepresentation;

    /**
     * Constructor providing the {@code String} representation of this {@code DAOPropagationState}.
     * 
     * @param stringRepresentation  A {@code String} corresponding to this {@code DAOPropagationState}.
     */
    private DAOPropagationState(String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
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
