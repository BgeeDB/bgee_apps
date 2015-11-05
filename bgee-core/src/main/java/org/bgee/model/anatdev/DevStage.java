package org.bgee.model.anatdev;

import org.bgee.model.NamedEntity;

/**
 * Class describing developmental stages.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Nov. 2015
 * @since   Bgee 13
 */
public class DevStage extends NamedEntity {

    /** @see #getLevel() */
    private final Integer level;

    /**
     * Default constructor not public, an ID must always be provided, 
     * see {@link #DevStage(String)}.
     */
    //Constructor not public on purpose, suppress warnings
    @SuppressWarnings("unused")
    private DevStage() {
        this(null);
    }
    /**
     * Constructor providing the ID of this {@code DevStage}. 
     * {@code id} cannot be blank, otherwise an {@code IllegalArgumentException} is thrown. 
     * 
     * @param id    A {@code String} representing the ID of this {@code DevStage}.
     * @throws IllegalArgumentException     if {@code id} is blank. 
     */
    public DevStage(String id) throws IllegalArgumentException {
        this(id, null, null, null);
    }
    /**
     * Constructor providing the ID, name, and description corresponding to this {@code DevStage}. 
     * {@code id} cannot be blank, otherwise an {@code IllegalArgumentException} is thrown. 
     * 
     * @param id            A {@code String} representing the ID of this {@code DevStage}.
     * @param name          A {@code String} representing the name of this {@code DevStage}.
     * @param description   A {@code String} representing the description of this {@code DevStage}.
     * @param level         An {@code Integer} representing the level of this {@code DevStage}
     *                      in its nested set model.
     * @throws IllegalArgumentException     if {@code id} is blank. 
     */
    public DevStage(String id, String name, String description, Integer level) {
        super(id, name, description);
        this.level = level;
    }
    
    /**
     * @return  The {@code Integer} representing the level of this {@code DevStage}
     *          in its nested set model.
     */
    public Integer getLevel() {
        return level;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((level == null) ? 0 : level.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        DevStage other = (DevStage) obj;
        if (level == null) {
            if (other.level != null)
                return false;
        } else if (!level.equals(other.level))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + " - Level: " + getLevel();
    }
}
