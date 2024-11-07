package org.bgee.model;

import java.util.Comparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A {@code NamedEntity} part of a nested set model.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Nov. 2015
 * @since Bgee 13 Nov. 2015
 */
public abstract class NestedSetModelEntity<T extends Comparable<T>> extends NamedEntity<T>
implements Comparable<NestedSetModelEntity<T>> {
    private final static Logger log = LogManager.getLogger(NestedSetModelEntity.class.getName());
    
    /**
     * A {@code Comparator} for {@code NestedSetModelEntity}.
     */
    public static final Comparator<NestedSetModelEntity<?>> nestedSetModelComparator = 
            Comparator.comparing(entity -> {
                if (entity.getLeftBound() == 0) {
                    throw log.throwing(new IllegalStateException("A left bound must be set "
                            + "in order to compare " + entity.getClass()));
                }
                return entity.getLeftBound();
            });
    
    /**
     * @see #getLeftBound()
     */
    private final int leftBound;
    /**
     * @see #getRightBound()
     */
    private final int rightBound;
    /**
     * @see #getLevel()
     */
    private final int level;

    /**
     * Constructor providing all parameters of a {@code NestedSetModelEntity}.
     * 
     * @param id            A {@code T} representing the ID of this entity. Cannot be blank.
     * @param name          A {@code String} that is the name of this entity.
     * @param description   A {@code String} that is the description of this entity.
     * @param leftBound     An {@code int} that is the left bound of this entity 
     *                      in its nested set model. First left bound is 1.
     * @param rightBound    An {@code int} that is the right bound of this entity 
     *                      in its nested set model.
     * @param level         An {@code int} that is the level of this entity 
     *                      in its nested set model. First level is 1.
     */
    protected NestedSetModelEntity(T id, String name, String description, 
            int leftBound, int rightBound, int level) {
        super(id, name, description);
        this.leftBound = leftBound;
        this.rightBound = rightBound;
        this.level = level;
    }
    
    /**
     * @return  An {@code int} that is the left bound of this element in its nested set model.
     *          First left bound is 1.
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
     *          First level is 1.
     */
    public int getLevel() {
        return level;
    }
    
    @Override
    public int compareTo(NestedSetModelEntity<T> entity) {
        log.entry(entity);
        return log.traceExit(nestedSetModelComparator.compare(this, entity));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString())
                .append(", leftBound=").append(leftBound)
                .append(", rightBound=").append(rightBound)
                .append(", level=").append(level);
        return builder.toString();
    }
    
}
