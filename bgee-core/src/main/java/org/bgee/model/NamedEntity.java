package org.bgee.model;

/**
 * Represents an {@code Entity} that can be named, and that also often has a description.
 * An example of the difference between a {@code NamedEntity} and an {@code Entity}  
 * could be for instance between a {@code Gene} (genes have an ID, a name, a description) 
 * and a {@code Call} (calls only have an ID, used to reference them).
 * <p>
 * Note that {@code equals} and {@code hashCode} methods of {@code NamedEntity}s 
 * should be solely based on their ID provided at instantiation.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Sept. 2015
 * @since Bgee 13 Sept. 2015
 * 
 * @param <T> The type of ID of this {@code NamedEntity}.
 */
public abstract class NamedEntity<T> extends Entity<T> {

    /**
     * @see #getName()
     */
    private final String name;
    /**
     * @see getDescription()
     */
    private final String description;
    
    /**
     * Default constructor not public, at lest an ID must always be provided, 
     * see {@link #NamedEntity(String)}.
     */
    //Constructor not public on purpose, suppress warnings
    @SuppressWarnings("unused")
    private NamedEntity() {
        this(null);
    }
    /**
     * Constructor providing the ID of this {@code NamedEntity}. 
     * {@code id} cannot be blank, otherwise an {@code IllegalArgumentException} is thrown. 
     * 
     * @param id    A {@code String} representing the ID of this {@code NamedEntity}.
     * @throws IllegalArgumentException     if {@code id} is blank. 
     */
    protected NamedEntity(T id) {
        this(id, null, null);
    }
    /**
     * Constructor providing the ID, the name, and the description of this {@code NamedEntity}. 
     * {@code id} cannot be blank, otherwise an {@code IllegalArgumentException} is thrown. 
     * Other arguments can be blank.
     * 
     * @param id            A {@code T} representing the ID of this {@code NamedEntity}. 
     *                      Cannot be blank.
     * @param name          A {@code String} that is the name of this {@code NamedEntity}.
     * @param description   A {@code String} that is the description of this {@code NamedEntity}.
     * @throws IllegalArgumentException     if {@code id} is blank. 
     */
    protected NamedEntity(T id, String name, String description) {
        super(id);
        this.name = name;
        this.description = description;
    }
    

    /**
     * @return  A {@code String} representing the name of this {@code NamedEntity}
     */
    public String getName() {
        return name;
    }
    /**
     * @return  A {@code String} that is a description of this {@code NamedEntity}
     */
    public String getDescription() {
        return description;
    }

	//we based hashCode/equals of NamedEntity on methods from parent class Entity:
    //only the ID matter for NamedEntity, as for Entity.

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NamedEntity [id=").append(getId())
               .append(", name=").append(name)
               .append(", description=").append(description)
               .append("]");
        return builder.toString();
    }
}
