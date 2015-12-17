package org.bgee.model.anatdev;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.NestedSetModelEntity;
import org.bgee.model.ontology.OntologyElement;

/**
 * Class describing developmental stages.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Nov. 2015
 * @since   Bgee 13
 */ 
public class DevStage extends NestedSetModelEntity implements OntologyElement<DevStage> {
    private final static Logger log = LogManager.getLogger(DevStage.class.getName());
    
    /**
     * @see #isTooGranular()
     */
    private final boolean tooGranular;
    /**
     * @see #isGroupingStage()
     */
    private final boolean groupingStage;

    /**
     * Constructor providing the ID of this {@code DevStage}. 
     * {@code id} cannot be blank, otherwise an {@code IllegalArgumentException} is thrown. 
     * 
     * @param id    A {@code String} representing the ID of this {@code DevStage}.
     * @throws IllegalArgumentException     if {@code id} is blank. 
     */
    public DevStage(String id) throws IllegalArgumentException {
        this(id, null, null, 0, 0, 0, false, false);
    }
    /**
     * Constructor providing all parameters of this {@code DevStage}. 
     * 
     * @param id            A {@code String} representing the ID of this entity. 
     *                      Cannot be blank.
     * @param name          A {@code String} that is the name of this entity.
     * @param description   A {@code String} that is the description of this entity.
     * @param leftBound     An {@code int} that is the left bound of this entity 
     *                      in its nested set model. First left bound is 1.
     * @param rightBound    An {@code int} that is the right bound of this entity 
     *                      in its nested set model.
     * @param level         An {@code int} that is the level of this entity 
     *                      in its nested set model. First level is 1.
     * @param tooGranular   A {@code Boolean} defining whether this stage is a highly granular 
     *                      developmental stage (for instance, 93 year-old). Such stages are 
     *                      usually not used in expression summaries, and are replaced 
     *                      by their closest parent not too granular.
     * @param groupingStage A {@code Boolean} defining whether this stage is a grouping stage, 
     *                      broad enough to allow comparisons of anatomical features. For instance, 
     *                      to compare expression in brain at stages such as "child", "early adulthood", 
     *                      "late adulthood", rather than at stages such as "23 yo", "24yo", "25yo", ...
     */
    public DevStage(String id, String name, String description, 
            int leftBound, int rightBound, int level, boolean tooGranular, boolean groupingStage) {
        super(id, name, description, leftBound, rightBound, level);
        if (tooGranular && groupingStage) {
            throw log.throwing(new IllegalArgumentException("A stage cannot be too granular "
                    + "and a grouping stage at the same time."));
        }
        this.tooGranular = tooGranular;
        this.groupingStage = groupingStage;
    }
    
    /**
     * @return  A {@code Boolean} defining whether this stage is a highly granular 
     *          developmental stage (for instance, 93 year-old). Such stages are usually not used 
     *          in expression summaries, and are replaced by their closest parent not too granular.
     */
    public boolean isTooGranular() {
        return tooGranular;
    }
    /**
     * @return  A {@code Boolean} defining whether this stage is a grouping stage, 
     *          broad enough to allow comparisons of anatomical features. For instance, 
     *          to compare expression in brain at stages such as "child", "early adulthood", 
     *          "late adulthood", rather than at stages such as "23 yo", "24yo", "25yo", ...
     */
    public boolean isGroupingStage() {
        return groupingStage;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (groupingStage ? 1231 : 1237);
        result = prime * result + (tooGranular ? 1231 : 1237);
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DevStage other = (DevStage) obj;
        if (groupingStage != other.groupingStage) {
            return false;
        }
        if (tooGranular != other.tooGranular) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DevStage [").append(super.toString())
        .append(", tooGranular=").append(tooGranular)
        .append(", groupingStage=").append(groupingStage)
        .append("]");
        return builder.toString();
    }
}
