package org.bgee.model.species;

import org.bgee.model.NamedEntity;

/**
 * Class allowing to describe species used in Bgee.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 01
 */
//TODO: equals/hashCode/toString
public class Species extends NamedEntity {
    
    /**
     * 0-arg constructor private, at least an ID must be provided, see {@link #Species(String)}.
     */
    @SuppressWarnings("unused")
    private Species() {
        this(null);
    }
    /**
     * Constructor providing the {@code id} of this {@code Species}.
     * This {@code id} cannot be blank,
     * otherwise an {@code IllegalArgumentException} will be thrown.
     *
     * @param id    A {@code String} representing the ID of this object.
     * @throws IllegalArgumentException if {@code id} is blank.
     */
    public Species(String id) throws IllegalArgumentException {
        this(id, null, null);
    }
    /**
     * Constructor of {@code Species}.
     * @param id            A {@code String} representing the ID of this {@code Species}. 
     *                      Cannot be blank.
     * @param name          A {@code String} representing the name of this {@Species}.
     * @param description   A {@code String} description of this {@Species}.
     */
    public Species(String id, String name, String description) throws IllegalArgumentException {
        super(id, name, description);
    }
}

