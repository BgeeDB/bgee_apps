package org.bgee.model.dao.api.ontologycommon;

import org.bgee.model.dao.api.EntityTO;

/**
 * Represents an {@code EntityTO} used as part of a nested set model.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 01
 */
public class NestedSetModelElementTO extends EntityTO {
    private static final long serialVersionUID = 3717417541660503259L;
    /**
     * An {@code int} that is the left bound of this element in its nested set model.
     */
    private final int leftBound;
    /**
     * An {@code int} that is the right bound of this element in its nested set model.
     */
    private final int rightBound;
    /**
     * An {@code int} that is the level of this element in its nested set model.
     */
    private final int level;

    /**
     * Constructor providing the ID
     * and its left bound, right bound, and level in its nested set model. 
     * <p>
     * All of these parameters are optional except {@code id}, so they can be 
     * {@code null} when not used (or equal to 0 for {@code int} arguments).
     * 
     * @param id                A {@code String} that is the ID.
     * @param leftBound         An {@code int} that is the left bound of this element 
     *                          in its nested set model.
     * @param rightBound        An {@code int} that is the right bound of this element 
     *                          in its nested set model.
     * @param level             An {@code int} that is the level of this element 
     *                          in its nested set model.
     * @throws IllegalArgumentException If {@code id} is {@code null} or empty, or if any of 
     *                                  {code leftBound} or {code rightBound} or {code level} 
     *                                  is less than 0.
     */
    public NestedSetModelElementTO(String id, 
            int leftBound, int rightBound, int level) throws IllegalArgumentException {
        this(id, null, null, leftBound, rightBound, level);
    }

    /**
     * Constructor providing the ID, the common name, and the description of the element, 
     * and its left bound, right bound, and level in its nested set model. 
     * <p>
     * All of these parameters are optional except {@code id}, so they can be 
     * {@code null} when not used (or equal to 0 for {@code int} arguments).
     * 
     * @param id                A {@code String} that is the ID.
     * @param name        A {@code String} that is the common name of this element.
     * @param description       A {@code String} that is the description for this element.
     * @param leftBound         An {@code int} that is the left bound of this element 
     *                          in its nested set model.
     * @param rightBound        An {@code int} that is the right bound of this element 
     *                          in its nested set model.
     * @param level             An {@code int} that is the level of this element 
     *                          in its nested set model.
     * @throws IllegalArgumentException If {@code id} is {@code null} or empty, or if any of 
     *                                  {code leftBound} or {code rightBound} or {code level} 
     *                                  is less than 0.
     */
    public NestedSetModelElementTO(String id, String name, String description, 
            int leftBound, int rightBound, int level) throws IllegalArgumentException {
        super(id, name, description);
        if (leftBound < 0 || rightBound < 0 || level < 0) {
            throw new IllegalArgumentException("Integer parameters must be positive.");
        }
        this.leftBound = leftBound;
        this.rightBound = rightBound;
        this.level = level;
    }

    /**
     * @return  An {@code int} that is the left bound of this element in its nested set model.
     */
    public int getLeftBound() {
        return leftBound;
    }
    /**
     * @return  An {@code int} that is the right bound of this element in its nested set model.
     */
    public int getRightBound() {
        return rightBound;
    }
    /**
     * @return  An {@code int} that is the level of this element in its nested set model.
     */
    public int getLevel() {
        return level;
    }
}
