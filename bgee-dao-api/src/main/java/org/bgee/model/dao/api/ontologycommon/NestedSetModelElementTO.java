package org.bgee.model.dao.api.ontologycommon;

import org.bgee.model.dao.api.NamedEntityTO;

/**
 * Represents an {@code EntityTO} used as part of a nested set model.
 * 
 * @author Frederic Bastian
 * @version Bgee 14 Feb. 2017
 * @since Bgee 01
 * 
 * @param <T> The type of ID of this {@code NamedEntityTO}.
 */
public class NestedSetModelElementTO<T extends Comparable<T>> extends NamedEntityTO<T> {
    private static final long serialVersionUID = 3717417541660503259L;
    /**
     * An {@code Integer} that is the left bound of this element in its nested set model.
     */
    private final Integer leftBound;
    /**
     * An {@code Integer} that is the right bound of this element in its nested set model.
     */
    private final Integer rightBound;
    /**
     * An {@code Integer} that is the level of this element in its nested set model.
     */
    private final Integer level;

    /**
     * Constructor providing the ID and its left bound, right bound, 
     * and level in its nested set model. 
     * <p>
     * All of these parameters are optional except {@code id}, so they can be {@code null} 
     * when not used. If an {@code Integer} is not {@code null} it should be positive.
     * 
     * @param id                A {@code T} that is the ID.
     * @param leftBound         An {@code Integer} that is the left bound of this element 
     *                          in its nested set model.
     * @param rightBound        An {@code Integer} that is the right bound of this element 
     *                          in its nested set model.
     * @param level             An {@code Integer} that is the level of this element 
     *                          in its nested set model.
     * @throws IllegalArgumentException If any of {code leftBound} or {code rightBound} or 
     *                                  {code level} is not {@code null} and less than 0.
     */
    public NestedSetModelElementTO(T id, Integer leftBound, Integer rightBound, Integer level) 
            throws IllegalArgumentException {
        this(id, null, null, leftBound, rightBound, level);
    }

    /**
     * Constructor providing the ID, the common name, and the description of the element, 
     * and its left bound, right bound, and level in its nested set model. 
     * <p>
     * All of these parameters are optional, so they can be {@code null} when not used.
     * If {code leftBound} or {code rightBound} or {code level} is not {@code null},
     * it should be positive.
     * 
     * @param id                A {@code T} that is the ID.
     * @param name              A {@code String} that is the common name of this element.
     * @param description       A {@code String} that is the description for this element.
     * @param leftBound         An {@code Integer} that is the left bound of this element 
     *                          in its nested set model.
     * @param rightBound        An {@code Integer} that is the right bound of this element 
     *                          in its nested set model.
     * @param level             An {@code Integer} that is the level of this element 
     *                          in its nested set model.
     * @throws IllegalArgumentException If any of {code leftBound} or {code rightBound} or 
     *                                  {code level} is not {@code null} and less than 0.
     */
    public NestedSetModelElementTO(T id, String name, String description, 
            Integer leftBound, Integer rightBound, Integer level) throws IllegalArgumentException {
        super(id, name, description);
        if ((leftBound !=null && leftBound < 0) || 
            (rightBound  != null && rightBound < 0) ||
            (level != null && level < 0)) {
            throw new IllegalArgumentException("Integer parameters must be positive.");
        }
        this.leftBound = leftBound;
        this.rightBound = rightBound;
        this.level = level;
    }

    /**
     * @return  An {@code Integer} that is the left bound of this element in its nested set model.
     */
    public Integer getLeftBound() {
        return leftBound;
    }
    /**
     * @return  An {@code Integer} that is the right bound of this element in its nested set model.
     */
    public Integer getRightBound() {
        return rightBound;
    }
    /**
     * @return  An {@code Integer} that is the level of this element in its nested set model.
     */
    public Integer getLevel() {
        return level;
    }
}
