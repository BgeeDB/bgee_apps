package org.bgee.model;

import org.apache.commons.lang3.StringUtils;

/**
 * Parent class of all classes corresponding to real entities in the Bgee database. 
 * For instance, a {@code Gene}, a {@code Species}, 
 * a {@code RNASeqExperiment}, ... Basically, anything that can have an ID.
 * <p>
 * Note that {@code equals} and {@code hashCode} methods of {@code Entity}s 
 * should be solely based on their ID provided at instantiation.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Sept. 2015
 * @since Bgee 01
 */
public abstract class Entity {
	/**
	 * @see #getId()
	 */
    private final String id;
    
    /**
     * Default constructor not public, at least an ID must always be provided, 
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
        if (StringUtils.isBlank(id)) {
            throw new IllegalArgumentException("the ID provided cannot be blank.");
        }
        this.id = id;
    }
    
    
	/**
	 * @return 	A {@code String} representing the ID of this {@code Entity}
	 */
	public String getId() {
		return this.id;
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
