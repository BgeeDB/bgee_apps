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
 */
public abstract class NamedEntity extends Entity {

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
    protected NamedEntity(String id) {
        this(id, null, null);
    }
    /**
     * Constructor providing the ID, the name, and the description of this {@code NamedEntity}. 
     * {@code id} cannot be blank, otherwise an {@code IllegalArgumentException} is thrown. 
     * Other arguments can be blank.
     * 
     * @param id            A {@code String} representing the ID of this {@code NamedEntity}. 
     *                      Cannot be blank.
     * @param name          A {@code String} that is the name of this {@code NamedEntity}.
     * @param description   A {@code String} that is the description of this {@code NamedEntity}.
     * @throws IllegalArgumentException     if {@code id} is blank. 
     */
    protected NamedEntity(String id, String name, String description) {
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
    
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		NamedEntity other = (NamedEntity) obj;
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}
	
    @Override
    public String toString() {
        return "ID: "+ getId() + " - Name: " + getName() + " - Description: " + getDescription();
    }
}
