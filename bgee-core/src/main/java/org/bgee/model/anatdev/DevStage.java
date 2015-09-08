package org.bgee.model.anatdev;

import org.bgee.model.Entity;

/**
 * Class describing developmental stages.
 * 
 * @author Frederic Bastian
 * @version Bgee 13.1
 *
 */
public class DevStage extends Entity {

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
        this(id, null, null);
    }
    /**
     * Constructor providing the ID, name, and description corresponding to this {@code DevStage}. 
     * {@code id} cannot be blank, otherwise an {@code IllegalArgumentException} is thrown. 
     * 
     * @param id            A {@code String} representing the ID of this {@code DevStage}.
     * @param name          A {@code String} representing the name of this {@code DevStage}.
     * @param description   A {@code String} representing the description of this {@code DevStage}.
     * @throws IllegalArgumentException     if {@code id} is blank. 
     */
    public DevStage(String id, String name, String description) {
        super(id, name, description);
    }

}
