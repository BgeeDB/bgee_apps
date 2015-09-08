package org.bgee.model.anatdev;

import org.bgee.model.Entity;

/**
 * Class describing anatomical entities.
 * 
 * @author Frederic Bastian
 * @version Bgee 13.1
 *
 */
public class AnatEntity extends Entity {

    /**
     * Default constructor not public, an ID must always be provided, 
     * see {@link #AnatEntity(String)}.
     */
    //Constructor not public on purpose, suppress warnings
    @SuppressWarnings("unused")
    private AnatEntity() {
        this(null);
    }
    /**
     * Constructor providing the ID of this {@code AnatEntity}. 
     * {@code id} cannot be blank, otherwise an {@code IllegalArgumentException} is thrown. 
     * 
     * @param id    A {@code String} representing the ID of this {@code AnatEntity}.
     * @throws IllegalArgumentException     if {@code id} is blank. 
     */
    public AnatEntity(String id) throws IllegalArgumentException {
        this(id, null, null);
    }
    /**
     * Constructor providing the ID, name, and description corresponding to this {@code AnatEntity}. 
     * {@code id} cannot be blank, otherwise an {@code IllegalArgumentException} is thrown. 
     * 
     * @param id            A {@code String} representing the ID of this {@code AnatEntity}.
     * @param name          A {@code String} representing the name of this {@code AnatEntity}.
     * @param description   A {@code String} representing the description of this {@code AnatEntity}.
     * @throws IllegalArgumentException     if {@code id} is blank. 
     */
    public AnatEntity(String id, String name, String description) {
        super(id, name, description);
    }
}
