package org.bgee.model;

import org.apache.commons.lang3.StringUtils;

/**
 * Parent class of all classes corresponding to real entities in the Bgee database. 
 * For instance, a {@code Gene}, a {@code Species}, 
 * a {@code RNASeqExperiment}, ...
 * They almost always have an ID, a name, and a description.
 * 
 * @author Frederic Bastian
 * @version Bgee 13.1
 * @since Bgee 01
 */
public abstract class Entity {
	/**
	 * A {@code String} representing the ID of this {@code Entity}. 
	 * All {@code Entity}s have an {@code id} which is immutable 
	 * and provided at instantiation. 
	 */
    private final String id;
	/**
	 * A {@code String} representing the name of this {@code Entity}.
	 */
    private final String name;
    /**
	 * A {@code String} that is a description of this {@code Entity}.
	 */
    private final String description;
    
    
    /**
     * Default constructor not public, an {@link #id} must always be provided, 
     * see {@link #Entity(String)}.
     */
    //Constructor not public on purpose, suppress warnings
    @SuppressWarnings("unused")
	private Entity() {
    	this(null);
    }
    /**
     * Constructor providing the ID of this {@code Entity}. 
     * {@code id} cannot be blank, otherwise an {@code IllegalArgumentException} is thrown. 
     * 
     * @param id	A {@code String} representing the ID of this {@code Entity}.
     * @throws IllegalArgumentException 	if {@code id} is blank. 
     */
    public Entity(String id) throws IllegalArgumentException {
    	this(id, null, null);
    }
    /**
     * Constructor providing the ID, name, and description corresponding to this {@code Entity}. 
     * {@code id} cannot be blank, otherwise an {@code IllegalArgumentException} is thrown. 
     * 
     * @param id            A {@code String} representing the ID of this {@code Entity}.
     * @param name          A {@code String} representing the name of this {@code Entity}.
     * @param description   A {@code String} representing the description of this {@code Entity}.
     * @throws IllegalArgumentException     if {@code id} is blank. 
     */
    public Entity(String id, String name, String description) {
        if (StringUtils.isBlank(id)) {
            throw new IllegalArgumentException("the ID provided cannot be blank");
        }
        this.id = id;
        this.name = name;
        this.description = description;
    }
    
    
	/**
	 * Gets the ID of this {@code Entity}.
	 * All {@code Entity}s have an {@code id} which is immutable 
	 * and provided at instantiation.
	 * @return 	A {@code String} representing the ID of this {@code Entity}
	 */
	public String getId() {
		return this.id;
	}
	/**
	 * Gets the name of this {@code Entity}.
	 * @return 	A {@code String} representing the name of this {@code Entity}
	 */
	public String getName() {
		return name;
	}
	/**
	 * Gets the description for this {@code Entity}.
	 * @return 	A {@code String} that is a description of this {@code Entity}
	 */
	public String getDescription() {
		return description;
	}
	
	/**
     * This {@code hashCode} method is solely based on the {@link #getId()} value.
     * {@inheritDoc}
     */
	@Override
	public int hashCode() {
		return ((id == null) ? 0 : id.hashCode());
	}
	/**
	 * This {@code equals} method is solely based on the {@link #getId()} value.
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Entity other = (Entity) obj;
		if (id == null) {
			if (other.getId() != null) {
				return false;
			}
		} else if (!id.equals(other.getId())) {
			return false;
		}
		return true;
	}
}
