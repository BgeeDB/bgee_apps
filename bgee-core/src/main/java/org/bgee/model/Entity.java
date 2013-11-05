package org.bgee.model;

import org.apache.commons.lang3.StringUtils;

/**
 * Parent class of all classes corresponding to real entities in the Bgee database. 
 * For instance, a {@code Gene}, a {@code Species}, 
 * a {@code RNASeqExperiment}, ...
 * They almost all have an ID, a name, and a description.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 01
 */
public abstract class Entity 
{
	/**
	 * A {@code String} representing the ID of this {@code Entity}. 
	 * All {@code Entity}s have an {@code id} which is immutable 
	 * and provided at instantiation. 
	 */
    private final String id;
	/**
	 * A {@code String} representing the name of this {@code Entity}.
	 */
    private String name;
    /**
	 * A {@code String} that is a description of this {@code Entity}.
	 */
    private String description;
    
    
    /**
     * Default constructor not public, an {@link #id} must always be provided, 
     * see {@link #Entity(String)}.
     */
    //Constructor not public on purpose, suppress warnings
    @SuppressWarnings("unused")
	private Entity()
    {
    	this(null);
    }
    /**
     * Constructor providing the {@code id} of this {@code Entity}. 
     * This {@code id} cannot be {@code null}, or empty (""), 
     * or whitespaces only, otherwise an {@code IllegalArgumentException} 
     * will be thrown. The ID will also be immutable, see {@link #getId()}.
     * 
     * @param id	A {@code String} representing the ID of this {@code Entity}.
     * @throws IllegalArgumentException 	if {@code id} is {@code null},  
     * 										empty, or whitespaces only. 
     */
    public Entity(String id) throws IllegalArgumentException {
    	if (StringUtils.isBlank(id)) {
    		throw new IllegalArgumentException("id cannot be null, or empty (\"\"), " +
    				"or whitespaces only");
    	}
    	this.id = id;
    	this.setName(null);
    	this.setDescription(null);
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
	 * Sets the name of this {@code Entity}.
	 * @param name A {@code String} that is the name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the description for this {@code Entity}.
	 * @return 	A {@code String} that is a description of this {@code Entity}
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * Sets the description of this {@code Entity}.
	 * @param description A {@code String} that is the description to set. 
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	@Override
	public int hashCode() {
		return ((id == null) ? 0 : id.hashCode());
	}
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
